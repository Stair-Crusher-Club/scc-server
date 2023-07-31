provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "iam.tfstate"
    region = "ap-northeast-2"
  }
}

