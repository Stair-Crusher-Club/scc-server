resource "aws_ecrpublic_repository" "scc_server" {
  repository_name = "scc-server"
  catalog_data {
    architectures     = ["ARM", "x86-64"]
    operating_systems = ["Linux"]
  }
}

