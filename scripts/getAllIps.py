import boto.ec2
import time
        
def get_all_ip(conn, ids):
    spots = conn.get_all_spot_instance_requests()
    return [spot.instance_id for spot in spots if spot.instance_id != None]

def get_all_running_ips():
    conn = boto.ec2.connect_to_region('us-east-1')
    # spots = conn.get_all_spot_instance_requests()
    # running_instances = [spot.instance_id for spot in spots if spot.instance_id != None]
    # # print running_instances
    # instances = conn.get_all_instances(instance_ids=running_instances)
    instances = conn.get_all_instances()
    ips = []
    for resv in instances:
        for instance in resv.instances:
            if instance.state == "running":
                ips.append(instance.public_dns_name)
            
    print "\n".join(ips)


if __name__ == '__main__':
    get_all_running_ips()