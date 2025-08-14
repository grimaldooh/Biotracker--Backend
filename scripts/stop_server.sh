#!/bin/bash
echo "Stopping backend service..."
pkill -f 'backend-0.0.1-SNAPSHOT.jar' || true