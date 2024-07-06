output "lb_dns_name" {
  value = aws_lightsail_lb.scc_lb.dns_name
}

output "k3s_ip" {
  value = aws_lightsail_instance.k3s_control_plane.public_ip_address
}
