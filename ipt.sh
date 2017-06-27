while read p; do
  echo ssh -t ec2-user@$p \"sudo service iptables stop\"
done < $1
