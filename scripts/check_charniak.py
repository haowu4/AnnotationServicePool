import subprocess
import sys

pl = subprocess.Popen(['ps', '-U', '0'], stdout=subprocess.PIPE).communicate()[0]

for line in pl.split("\n"):
    line = line.strip()
    if len(line) == 0:
        continue
    # proc_name = line.split()[-1]
    # print proc_name
    if "charniak9987" in line:
        print "exist charniak, stop scripts..."
        sys.exit(0)
        
print "Starting again..."
pl = subprocess.Popen(['bash', '/home/ec2-user/run/start_cp.sh'], stdout=subprocess.PIPE).communicate()[0]
