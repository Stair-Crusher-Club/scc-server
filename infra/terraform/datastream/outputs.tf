# output "datastream_stream_name" {
#   value = google_datastream_stream.scc_rds_to_bigquery.name
# }
output "datastream_user_password" {
  value     = random_password.scc_rds_datastream_user_password.result
  sensitive = true
}
