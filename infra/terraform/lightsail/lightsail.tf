resource "aws_lightsail_key_pair" "scc_key_pair" {
  name = "scc_key_pair"
}

locals {
  control_plane_userdata = <<USERDATA
#!/bin/bash
curl -sfL https://get.k3s.io | sh -s - server \
  --write-kubeconfig-mode "0644" \
  --token ${data.sops_file.secret_data.data["k3s.token"]}
USERDATA
}

resource "aws_lightsail_instance" "k3s_control_plane" {
  name              = "k3s_control_plane"
  availability_zone = "ap-northeast-2a"
  key_pair_name     = aws_lightsail_key_pair.scc_key_pair.name
  bundle_id         = "small_2_0"
  blueprint_id      = "ubuntu_20_04"
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
    from_port = 6443
    to_port   = 6443
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
}

locals {
  data_plane_userdata = <<USERDATA
#!/bin/bash
curl -sfL https://get.k3s.io | sh -s - agent \
  --server https://${aws_lightsail_instance.k3s_control_plane.private_ip_address}:6443 \
  --token ${data.sops_file.secret_data.data["k3s.token"]}
USERDATA
}

resource "aws_lightsail_instance" "k3s_data_plane" {
  name              = "k3s_data_plane_1"
  availability_zone = "ap-northeast-2c"
  key_pair_name     = aws_lightsail_key_pair.scc_key_pair.name
  bundle_id         = "small_2_0"
  blueprint_id      = "ubuntu_20_04"
  user_data         = local.data_plane_userdata
  tags = {
    role = "data_plane"
  }
}

resource "aws_lightsail_instance_public_ports" "k3s_data_plane" {
  instance_name = aws_lightsail_instance.k3s_data_plane.name

  port_info {
    protocol  = "tcp"
    from_port = 22
    to_port   = 22
    cidrs     = ["172.26.0.0/20"]
  }

  port_info {
    protocol  = "tcp"
    from_port = 6443
    to_port   = 6443
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
}
