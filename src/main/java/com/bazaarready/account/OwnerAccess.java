package com.bazaarready.account;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Decides who may manage a site. Every owner page and owner action goes through here: the visitor
 * must be signed in, and the site must belong to their account.
 *
 * <p>Sites made before accounts existed have no owner yet. The first signed-in person who opens
 * such a site's private owner link claims it, because only the owner was ever given that link.
 */
@Component
public class OwnerAccess {

    private final ProfileService profiles;
    private final AccountService accounts;
    private final JdbcTemplate jdbc;

    OwnerAccess(ProfileService profiles, AccountService accounts, JdbcTemplate jdbc) {
        this.profiles = profiles;
        this.accounts = accounts;
        this.jdbc = jdbc;
    }

    /** The site behind a private owner link, if it belongs to the signed-in account. */
    public ProfileResponse open(UUID ownerKey) {
        ProfileResponse shop = profiles.getByOwnerKey(ownerKey);
        check(shop.id());
        return shop;
    }

    /** Gives a new site to the signed-in person, if anyone is signed in. Visitors building without an account skip this. */
    public void claimIfSignedIn(UUID businessId) {
        if (accounts.currentId().isPresent()) {
            check(businessId);
        }
    }

    /**
     * For changing a site from the builder screen. A site nobody has claimed yet can be changed by
     * whoever has its private link, as before accounts existed. Once an owner has claimed it on the
     * dashboard, only that owner, signed in, can change it.
     */
    public ProfileResponse openForChange(UUID ownerKey) {
        ProfileResponse shop = profiles.getByOwnerKey(ownerKey);
        List<UUID> owner = jdbc.query("select account_id from business where id = ?", (rs, n) -> rs.getObject(1, UUID.class), shop.id());
        if (!owner.isEmpty() && owner.get(0) != null) {
            check(shop.id());
        }
        return shop;
    }

    /** Throws NotFoundException (shown as "not found", so nothing leaks) unless the signed-in account owns the site. */
    public void check(UUID businessId) {
        UUID me = accounts.requireCurrentId();
        List<UUID> owner = jdbc.query("select account_id from business where id = ?",
                (rs, n) -> rs.getObject(1, UUID.class), businessId);
        if (owner.isEmpty()) {
            throw new NotFoundException("No site at this address");
        }
        if (owner.get(0) == null) {
            jdbc.update("update business set account_id = ? where id = ? and account_id is null", me, businessId);
            UUID now = jdbc.queryForObject("select account_id from business where id = ?", UUID.class, businessId);
            if (!me.equals(now)) {
                throw new NotFoundException("This owner link is not valid");
            }
        } else if (!owner.get(0).equals(me)) {
            throw new NotFoundException("This owner link is not valid");
        }
    }
}
