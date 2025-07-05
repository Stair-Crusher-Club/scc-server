resource "aws_cloudwatch_metric_alarm" "scc_alarms" {
  for_each = var.alarms

  alarm_name          = each.value.alarm_name
  alarm_description   = each.value.alarm_description
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  threshold           = each.value.threshold
  period              = each.value.period
  unit                = each.value.unit
  namespace           = each.value.namespace
  metric_name         = each.value.metric_name
  statistic           = each.value.statistic

  actions_enabled = true
  alarm_actions = [aws_sns_topic.scc_alarm_sns_topic.arn]
  ok_actions = [aws_sns_topic.scc_alarm_sns_topic.arn]

  dimensions = {
    DBInstanceIdentifier = data.terraform_remote_state.database.outputs.scc_rds_instance_identifier
  }
}
