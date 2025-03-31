resource "aws_cloudwatch_metric_alarm" "scc_rds_cpu_utilization" {
  alarm_name          = "scc-rds-cpu-high"
  alarm_description   = "Triggers when CPU utilization of the RDS instance exceeds 80%"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  threshold           = 80
  period              = 60
  unit                = "Percent"
  namespace           = "AWS/RDS"
  metric_name         = "CPUUtilization"
  statistic           = "Average"

  actions_enabled = true
  alarm_actions = [aws_sns_topic.scc_alarm_sns_topic.arn]
  ok_actions = [aws_sns_topic.scc_alarm_sns_topic.arn]

  dimensions = {
    DBInstanceIdentifier = data.terraform_remote_state.database.outputs.scc_rds_instance_identifier
  }
}