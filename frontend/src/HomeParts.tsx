import { HomeStrings, STRIP_LINKS } from "./home";
import { FeaturedSite } from "./showcase";

/** A real published site in a browser frame, with the result of its latest AI customer test. */
export function FeaturedPreview({ site, h }: { site: FeaturedSite | null; h: HomeStrings }) {
  if (!site) return null;
  const host = window.location.host || "bazaarready.com";
  return (
    <section className="featured" data-reveal aria-label={site.name}>
      <p className="hand featured-note">{h.whatYouGet}</p>
      <a className="browser" href={site.path} target="_blank" rel="noreferrer">
        <span className="browser-bar" aria-hidden="true">
          <i />
          <i />
          <i />
          <span className="browser-url">
            {host}
            {site.path}
          </span>
        </span>
        <span className="browser-body">
          <img className="browser-hero" src={site.photo.url} alt="" loading="lazy" />
          <span className="browser-text">
            <span className="browser-name">{site.name}</span>
            {site.tagline && <span className="browser-tagline">{site.tagline}</span>}
            {site.items.length > 0 && (
              <span className="browser-items">
                {site.items.map((item) => (
                  <span key={item.name} className="browser-item">
                    {item.photo ? <img src={item.photo.url} alt="" loading="lazy" /> : <span className="browser-item-blank" />}
                    <strong>{item.name}</strong>
                    {item.price && <em>{item.price}</em>}
                  </span>
                ))}
              </span>
            )}
            <span className="browser-open">{h.openSite}</span>
          </span>
        </span>
      </a>
      {site.passed != null && site.total != null && (
        <p className="featured-badge">
          <strong>
            {h.badge
              .replace("{passed}", String(site.passed))
              .replace("{total}", String(site.total))}
          </strong>
        </p>
      )}
    </section>
  );
}

/** Four short cards, each opening its own page: templates, how it works, features. */
export function LinksStrip({ h }: { h: HomeStrings }) {
  return (
    <nav className="strip" aria-label={h.features}>
      {h.strip.map((card, i) => (
        <a key={card.big} className="strip-card" data-reveal href={STRIP_LINKS[i]}>
          <strong>{card.big}</strong>
          <span>{card.small}</span>
          <em>{card.link} →</em>
        </a>
      ))}
    </nav>
  );
}
