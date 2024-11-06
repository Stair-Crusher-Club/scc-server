resource "aws_lightsail_key_pair" "scc_key_pair" {
  name = "scc_key_pair"
}

locals {
  control_plane_userdata = <<USERDATA
#!/bin/bash
mkdir -p /var/lib/rancher/k3s/server/manifests
NODE_PORT=${var.node_port}
echo "${data.sops_file.secret_data.data["k3s.traefik-config"]}" > /var/lib/rancher/k3s/server/manifests/traefik-config.yaml
curl -sfL https://get.k3s.io | sh -s - server \
  --write-kubeconfig-mode "0644" \
  --token ${data.sops_file.secret_data.data["k3s.token"]} \
  --kube-apiserver-arg service-account-issuer=https://k3s.staircrusher.club \
  --kube-apiserver-arg service-account-jwks-uri=https://k3s.staircrusher.club/openid/v1/jwks \
  --kube-apiserver-arg anonymous-auth=true \
  --https-listen-port 443 \
  --disable servicelb
kubectl create clusterrolebinding oidc-reviewer --clusterrole=system:service-account-issuer-discovery --group=system:unauthenticated
USERDATA
}

resource "aws_lightsail_instance" "k3s_control_plane" {
  name              = "k3s_control_plane"
  availability_zone = "ap-northeast-2a"
  key_pair_name     = aws_lightsail_key_pair.scc_key_pair.name
  bundle_id         = "small_2_0"
  blueprint_id      = "ubuntu_22_04"
  user_data         = local.control_plane_userdata
  tags = {
    role = "control_plane"
  }
}

resource "aws_lightsail_instance_public_ports" "k3s_control_plane" {
  instance_name = aws_lightsail_instance.k3s_control_plane.name

  port_info {
    protocol  = "tcp"
    from_port = 22
    to_port   = 22
    cidrs     = ["0.0.0.0/0"]
  }

  port_info {
    protocol  = "tcp"
    from_port = var.node_port
    to_port   = var.node_port
    cidrs     = ["0.0.0.0/0"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 443
    to_port   = 443
    cidrs     = ["0.0.0.0/0"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 10250
    to_port   = 10250
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "udp"
    from_port = 8472
    to_port   = 8472
    cidrs     = ["172.26.0.0/20"]
  }

  lifecycle {
    replace_triggered_by = [
      aws_lightsail_instance.k3s_control_plane.id
    ]
  }
}

locals {
  data_plane_userdata = <<USERDATA
#!/bin/bash
curl -sfL https://get.k3s.io | sh -s - agent \
  --server https://${aws_lightsail_instance.k3s_control_plane.private_ip_address} \
  --token ${data.sops_file.secret_data.data["k3s.token"]}
USERDATA

  data_plane_userdata_v1_27_3 = <<USERDATA
#!/bin/bash
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION="v1.27.3+k3s1" sh -s - agent \
  --server https://${aws_lightsail_instance.k3s_control_plane.private_ip_address} \
  --token ${data.sops_file.secret_data.data["k3s.token"]}
USERDATA
}

resource "aws_lightsail_instance" "k3s_data_planes" {
  for_each = { for i in range(3) : i => i }

  name              = "k3s_data_plane_${each.value}"
  availability_zone = element(["ap-northeast-2a", "ap-northeast-2c"], each.value % 2)
  key_pair_name     = aws_lightsail_key_pair.scc_key_pair.name
  bundle_id         = "medium_2_0"
  blueprint_id      = "ubuntu_22_04"
  user_data         = local.data_plane_userdata
  tags = {
    role = "data_plane"
  }
}

resource "aws_lightsail_instance_public_ports" "k3s_data_planes" {
  for_each = aws_lightsail_instance.k3s_data_planes

  instance_name = each.value.name

  port_info {
    protocol  = "tcp"
    from_port = 22
    to_port   = 22
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "tcp"
    from_port = var.node_port
    to_port   = var.node_port
    cidrs     = ["0.0.0.0/0"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 443
    to_port   = 443
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 10250
    to_port   = 10250
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "udp"
    from_port = 8472
    to_port   = 8472
    cidrs     = ["172.26.0.0/20"]
  }

  lifecycle {
    replace_triggered_by = [
      aws_lightsail_instance.k3s_data_planes[each.key].id
    ]
  }
}

resource "aws_lightsail_instance" "k3s_data_planes_v1_27_3" {
  for_each = { for i in range(2) : i => i }

  name              = "k3s_data_plane_v1_27_3_${each.value}"
  availability_zone = element(["ap-northeast-2a", "ap-northeast-2c"], each.value % 2)
  key_pair_name     = aws_lightsail_key_pair.scc_key_pair.name
  bundle_id         = "medium_2_0"
  blueprint_id      = "ubuntu_22_04"
  user_data         = local.data_plane_userdata_v1_27_3
  tags = {
    role = "data_plane"
  }
}

resource "aws_lightsail_instance_public_ports" "k3s_data_planes_v1_27_3" {
  for_each = aws_lightsail_instance.k3s_data_planes_v1_27_3

  instance_name = each.value.name

  port_info {
    protocol  = "tcp"
    from_port = 22
    to_port   = 22
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "tcp"
    from_port = var.node_port
    to_port   = var.node_port
    cidrs     = ["0.0.0.0/0"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 443
    to_port   = 443
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 10250
    to_port   = 10250
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "udp"
    from_port = 8472
    to_port   = 8472
    cidrs     = ["172.26.0.0/20"]
  }

  lifecycle {
    replace_triggered_by = [
      aws_lightsail_instance.k3s_data_planes[each.key].id
    ]
  }
}
