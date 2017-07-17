import boto.ec2
import time
import click

@click.command()
@click.option('--count', default=1, help='number of greetings')
@click.option('--init', default="../runCloud.sh")
def run_instance(count, init):
	conn = boto.ec2.connect_to_region('us-east-1')

	# Enhanced creation now with the addition of 'user_data'

	user_data_script = """#!/bin/bash
	echo "Hello World" >> /tmp/data.txt"""

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
	print new_reservation
	# Add a Name to the instance, then loop to wait for it to be running.
	# instance = new_reservation.instances[0]
	# conn.create_tags([instance.id], {"Name":"PyWebDev Example 3b"})
	# while instance.state == u'pending':
	#     print "Instance state: %s" % instance.state
	#     time.sleep(10)
	# #     instance.update()

	# print "Instance state: %s" % instance.state
	# print "Public dns: %s" % instance.public_dns_name
	 
if __name__ == '__main__':
	run_instance()