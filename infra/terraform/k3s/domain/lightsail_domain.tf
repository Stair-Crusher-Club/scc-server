resource "aws_lightsail_domain" "staircrusher_club" {
  domain_name = "staircrusher.club"
}

resource "aws_lightsail_domain" "dev_staircrusher_club" {
  domain_name = "dev.staircrusher.club"
}

# FIXME: does not work correctly due to terraform bug
# resource "aws_lightsail_domain_entry" "acm" {
#   for_each = {
#     "_8985d37115fba2afd63bce08349ac02e.api"       = "_731f54376d93d5d6c9db9f282aff9328.nbgfhbpblk.acm-validations.aws."
#     "_4cefeaf9c021e6eccfe0c23ca586dae1.admin-api" = "_9efd37d17364e7d51b2064a2ff1d252c.nbgfhbpblk.acm-validations.aws."
#     "_8c5fa176b85703fa7649368266c87855.redash"    = "_a3ed4e60185fa3fee606cc4ffaccfad3.nbgfhbpblk.acm-validations.aws."
#     "_9bbd6321f76af969e4a766a60ad3afab"           = "_b04d36eddbd638266d74cbddb60f739b.qqqkmlyjyg.acm-validations.aws."
#     "_d41aa41f61974f00401f545b5ec6b29b.www"       = "_18487efa2da29bad3865d9b3e88eb7ef.nbgfhbpblk.acm-validations.aws."
#     "_91b62c133bc56f61489e5f94a53d98cf.admin"     = "_a0e5dd70e11f8176d3867d44233b9cdd.nbgfhbpblk.acm-validations.aws."
#   }
#   domain_name = aws_lightsail_domain.staircrusher_club.domain_name
#   type        = "CNAME"
#   name        = each.key
#   target      = each.value
# }

# resource "aws_lightsail_domain_entry" "dev_acm" {
#   for_each = {
#     "_c731bd34f5765a4403c546f3e0a797f6.admin-api" = "_81df3ba184baa7473e134fe0525f9897.nbgfhbpblk.acm-validations.aws."
#     "_045e514bf062801eb27e5680e88406fc.admin"     = "_5784dbaf5eb751d626a676d40132ac48.nbgfhbpblk.acm-validations.aws."
#     "_c7a9c4753120d2aa3918b83195fe5459.api"       = "_4c8039f4d8b7fb8309a0a2e37361ba2d.nbgfhbpblk.acm-validations.aws."
#   }
#   domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
#   type        = "CNAME"
#   name        = each.key
#   target      = each.value
# }

resource "aws_lightsail_domain_entry" "dev_ns" {
  for_each = toset([
    "ns-136.awsdns-17.com",
    "ns-1918.awsdns-47.co.uk",
    "ns-1417.awsdns-49.org",
    "ns-918.awsdns-50.net",
  ])
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "dev"
  type        = "NS"
  target      = each.key
}

# FIXME: this entry should be created before creating oidc connector
resource "aws_lightsail_domain_entry" "k3s" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "k3s"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.k3s_ip
}

resource "aws_lightsail_domain_entry" "api" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "api"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "dev_api" {
  domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
  name        = "api"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "admin" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "admin"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "redash" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name        = "redash"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "dev_admin" {
  domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
  name        = "admin"
  type        = "A"
  target      = data.terraform_remote_state.lightsail.outputs.lb_dns_name
  is_alias    = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "dev_cdn" {
  domain_name = aws_lightsail_domain.dev_staircrusher_club.domain_name
  name = "static"
  type = "A"
  target = data.terraform_remote_state.scc.outputs.dev_cdn_endpoint
  is_alias = true

  lifecycle {
    create_before_destroy = false
  }
}

resource "aws_lightsail_domain_entry" "cdn" {
  domain_name = aws_lightsail_domain.staircrusher_club.domain_name
  name = "static"
  type = "A"
  target = data.terraform_remote_state.scc.outputs.cdn_endpoint
  is_alias = true

  lifecycle {
    create_before_destroy = false
  }
}
