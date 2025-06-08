CREATE TABLE IF NOT EXISTS push_notification_schedule (
    id VARCHAR(36) NOT NULL,
    scheduled_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    sent_at TIMESTAMP(6) WITH TIME ZONE NULL,
    title VARCHAR(255) NULL,
    body TEXT NOT NULL,
    deep_link VARCHAR(255) NULL,
    target_user_ids TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE INDEX idx_push_notification_schedule_scheduled_at ON push_notification_schedule(scheduled_at);
CREATE INDEX idx_push_notification_schedule_sent_at ON accessibility_image(sent_at);
CREATE INDEX idx_push_notification_schedule_created_at ON accessibility_image(created_at);