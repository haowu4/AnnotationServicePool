import boto.ec2
import time
import click


def all_have_ip(conn, ids):
    spots = conn.get_all_spot_instance_requests(request_ids=ids)
    for spot in spots:
        if spot.instance_id is None:
            return False
    return True
        
def get_all_ip(conn, ids):
    spots = conn.get_all_spot_instance_requests(request_ids=ids)
    return [spot.instance_id for spot in spots]

@click.command()
@click.option('--count', default=1, help='number of greetings')
@click.option('--init', default="../runCloud.sh")
def run_instance(count, init):
    conn = boto.ec2.connect_to_region('us-east-1')
 
    with open(init) as input_fd:
        user_data_script = input_fd.read()

    # Red Hat Enterprise Linux 6.4 (ami-7d0c6314)


    new_reservation = conn.request_spot_instances(
                            price="0.19",
                            image_id='ami-56fde840',
                            count=count,
                            type="one-time",
                            key_name='haowu4_mbpr',
                            instance_type='m4.2xlarge',
                            security_group_ids=['sg-33ce6642'],
                            user_data=user_data_script)
    print "New instance created."
    time.sleep(1)
    spot_ids = [x.id for x in new_reservation]
    i = 20
    while i > 0:
        if all_have_ip(conn, spot_ids):
            conn.create_tags(get_all_ip(conn, spot_ids), {"Name":"Curator with SRL"})
            break
        
        time.sleep(10)

    if i <= 0:
        print "Waited too long for spot instance to fire up..."

if __name__ == '__main__':
    run_instance()