resource "google_datastream_connection_profile" "scc_rds_datastream" {
  connection_profile_id = "scc-rds-datastream"
  display_name          = "scc_rds source connection profile for datastream"
  location              = var.region

  postgresql_profile {
    hostname = data.terraform_remote_state.database.outputs.scc_rds_instance_host
    port     = var.rds_postgres_port
    username = postgresql_role.scc_rds_datastream_user.name
    password = postgresql_role.scc_rds_datastream_user.password
    database = var.rds_postgres_database
  }
}

resource "google_datastream_connection_profile" "scc_bigquery" {
  connection_profile_id = "scc-bigquery"
  display_name          = "scc_bigquery connection profile"
  location              = var.region

  bigquery_profile {}
}
