provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  backend "s3" {
    bucket = "scc-tf-remote-state"
    key    = "secret-manager.tfstate"
    region = "ap-northeast-2"
  }
}
