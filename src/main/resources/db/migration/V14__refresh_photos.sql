-- Photos chosen before the better matching (main object required in the title) are searched again.
-- Pages look them up afresh in the background the next time they are shown.
delete from stock_photo where fetched_at < now();
