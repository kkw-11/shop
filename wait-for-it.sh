#!/bin/sh
# wait-for-it.sh - Wait for MySQL to be ready

set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

until nc -z "$host" "$port"; do
  echo "MySQL is unavailable - sleeping"
  sleep 2
done

echo "MySQL is up - starting application"
exec $cmd
