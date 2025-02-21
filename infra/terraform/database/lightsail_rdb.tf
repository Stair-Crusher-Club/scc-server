resource "aws_lightsail_database" "scc_db_v2" {
  relational_database_name = "scc-db-v2"
  availability_zone        = "ap-northeast-2a"
  master_database_name     = "scc"
  master_password          = data.sops_file.secret_data.data["db.password"]
  master_username          = "scc"
  blueprint_id             = "postgres_15"
  bundle_id                = "medium_2_0"
  skip_final_snapshot      = false
  final_snapshot_name      = "scc-db-v2-final-snapshot"
}

resource "awscc_lightsail_alarm" "rdb_cpu_high_alarm" {
  alarm_name              = "rdb_cpu_high_alarm"
  comparison_operator     = "GreaterThanThreshold"
  evaluation_periods      = 1
  metric_name             = "CPUUtilization"
  monitored_resource_name = aws_lightsail_database.scc_db_v2.relational_database_name
  threshold               = 90
  notification_triggers   = ["ALARM", "OK"]
  contact_protocols       = ["Email"]
}
