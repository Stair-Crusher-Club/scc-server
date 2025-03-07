resource "aws_s3_bucket" "prod_scc_hot_updater_storage" {
  bucket = "scc-hot-updater-storage"
}

resource "aws_s3_bucket" "dev_scc_hot_updater_storage" {
  bucket = "dev-scc-hot-updater-storage"
}
