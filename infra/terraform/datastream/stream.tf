resource "google_datastream_stream" "scc_rds_to_bigquery" {
  stream_id    = "scc-rds-to-bigquery"
  location     = var.region
  display_name = "scc_rds to BigQuery stream"

  source_config {
    source_connection_profile = google_datastream_connection_profile.scc_rds_datastream.name

    postgresql_source_config {
      publication = postgresql_publication.scc_rds_datastream_publication.name
      replication_slot = postgresql_replication_slot.scc_rds_datastream_replication_slot.name
    }
  }

  destination_config {
    destination_connection_profile = google_datastream_connection_profile.scc_bigquery.name

    bigquery_destination_config {
      data_freshness = "900s"  # Max staleness: 15 minutes

      source_hierarchy_datasets {
        dataset_template {
          location = var.region
          dataset_id_prefix = "scc_rds_"
        }
      }
    }
  }

  # Backfill all existing data initially
  backfill_all {}
}
