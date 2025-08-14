#!/bin/bash
echo "Starting backend service..."
nohup java -jar /home/ec2-user/biotrack/backend-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &