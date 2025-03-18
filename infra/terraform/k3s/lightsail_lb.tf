resource "aws_lightsail_lb" "scc_lb" {
  name              = "scc_lb"
  health_check_path = "/healthz"
  instance_port     = var.node_port
}

resource "aws_lightsail_lb_attachment" "k3s" {
  for_each = toset(
    concat(
      [aws_lightsail_instance.k3s_control_plane.name],
      [for k, v in aws_lightsail_instance.k3s_data_planes : v.name],
      [for k, v in aws_lightsail_instance.k3s_data_planes_v1_27_3 : v.name],
    )
  )
  lb_name       = aws_lightsail_lb.scc_lb.name
  instance_name = each.key
}

import {
  to = aws_lightsail_lb_certificate.scc_lb_cert
  id = "scc_lb,star.staircrusher.club"
}

// NOTE: 실제 DNS record는 AWS lightsail console에서 직접 수정해야 한다.
resource "aws_lightsail_lb_certificate" "scc_lb_cert" {
  name                      = "star.staircrusher.club"
  lb_name                   = aws_lightsail_lb.scc_lb.id
  domain_name               = "api.staircrusher.club"
  subject_alternative_names = [
    "admin.dev.staircrusher.club",
    "admin.staircrusher.club",
    "api.dev.staircrusher.club",
    "api.staircrusher.club",
    "redash.staircrusher.club",
    "grafana.staircrusher.club",
    "prometheus.staircrusher.club",
    "staircrusher.club",
    "www.staircrusher.club",
    "con.staircrusher.club",
  ]
}

import {
  to = aws_lightsail_lb_certificate_attachment.scc_lb_cert_attachment
  id = "scc_lb,star.staircrusher.club"
}

resource "aws_lightsail_lb_certificate_attachment" "scc_lb_cert_attachment" {
  lb_name          = aws_lightsail_lb.scc_lb.name
  certificate_name = aws_lightsail_lb_certificate.scc_lb_cert.name
}

import {
  to = aws_lightsail_lb_https_redirection_policy.scc_lb_https
  id = "scc_lb"
}

resource "aws_lightsail_lb_https_redirection_policy" "scc_lb_https" {
  lb_name = aws_lightsail_lb.scc_lb.name
  enabled = true
}
