provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "kms.tfstate"
    region = "ap-northeast-2"
  }
}

