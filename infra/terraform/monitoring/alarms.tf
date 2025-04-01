variable "alarms" {
  description = "Map of alarms for scc rds"
  type = map(object({
    alarm_name          = string
    alarm_description   = string
    comparison_operator = string
    evaluation_periods  = number
    threshold           = number
    period              = number
    unit                = optional(string)
    namespace           = string
    metric_name         = string
    statistic           = string
  }))

  default = {
    scc_rds_cpu_utilization = {
      alarm_name          = "SccRdsCpuHigh"
      alarm_description   = "Triggers when CPU utilization of the RDS instance exceeds 80%"
      comparison_operator = "GreaterThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 80
      period              = 60
      unit                = "Percent"
      namespace           = "AWS/RDS"
      metric_name         = "CPUUtilization"
      statistic           = "Average"
    }
    scc_rds_connection = {
      alarm_name          = "SccRdsTooManyConnections"
      alarm_description   = "Triggers when connections to the RDS instance exceed 350 (Max: 403)"
      comparison_operator = "GreaterThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 350
      period              = 60
      unit                = "Count"
      namespace           = "AWS/RDS"
      metric_name         = "DatabaseConnections"
      statistic           = "Average"
    }
    scc_rds_freeable_memory = {
      alarm_name          = "SccRdsFreeableMemoryLow"
      alarm_description   = "Triggers when freeable memory of the RDS instance is less than 500MB"
      comparison_operator = "LessThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 500000000
      period              = 60
      unit                = "Bytes"
      namespace           = "AWS/RDS"
      metric_name         = "FreeableMemory"
      statistic           = "Average"
    }
    scc_rds_write_latency = {
      alarm_name          = "SccRdsWriteLatencyHigh"
      alarm_description   = "Triggers when write latency of the RDS instance exceeds 100ms"
      comparison_operator = "GreaterThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 0.1
      period              = 60
      unit                = "Seconds"
      namespace           = "AWS/RDS"
      metric_name         = "WriteLatency"
      statistic           = "Average"
    }
    scc_rds_read_latency = {
      alarm_name          = "SccRdsReadLatencyHigh"
      alarm_description   = "Triggers when read latency of the RDS instance exceeds 50ms"
      comparison_operator = "GreaterThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 0.05
      period              = 60
      unit                = "Seconds"
      namespace           = "AWS/RDS"
      metric_name         = "ReadLatency"
      statistic           = "Average"
    }
    scc_rds_db_load_non_cpu = {
      alarm_name          = "SccRdsLoadNonCpuHigh"
      alarm_description   = "Triggers when average active sessions blocked by non-cpu event exceeds 1"
      comparison_operator = "GreaterThanOrEqualToThreshold"
      evaluation_periods  = 1
      threshold           = 1
      period              = 60
      unit                = null
      namespace           = "AWS/RDS"
      metric_name         = "DBLoadNonCPU"
      statistic           = "Average"
    }
  }
}
