#!/bin/bash
set -xe -o nounset

readonly SCRIPT_DIR=/home/dmacsuibhne/waratekRepos/qa-tools/scripts/ami_creation

function run_in_docker_container()
{
    local container_name=$1
    local bash_command=$2
    echo "Running command '$bash_command' in container '$container_name'"
    sudo docker exec "$container_name" /bin/bash -c "$bash_command"
}

function wait_for_docker_container_healthcheck() {
    # Only valid for containers which have healthchecks
    max_retries=300
    retries=0
    local container_name=$1
    echo "Waiting for container $container_name to be healthy"
    set +x # Don't log commands inside loop
    until sudo docker ps|grep "$container_name"| grep "(healthy)" ; do
        retries=$((retries + 1))
        if [ "$retries" -gt "$max_retries" ]; then
            echo "Container '$container_name' did not become healthy within '$max_retries' seconds";
            sudo docker ps -a
            exit 1
        fi
        sleep 1
    done
    sleep 10 # Small wait at the end to avoid rare timing issues
    set -x
}


# Setup oracle database based on https://container-registry.oracle.com/ords/f?p=113:4:128092208661230:::4:P4_REPOSITORY,AI_REPOSITORY,AI_REPOSITORY_NAME,P4_REPOSITORY_NAME,P4_EULA_ID,P4_BUSINESS_AREA_ID:1863,1863,Oracle%20Database%20Free,Oracle%20Database%20Free,1,0&cs=3nc5-csLQNq43uZYU8wq0_8Br5bMQdHCCG7wud6qco7ag7-PWE2w14KKsaLAineynkgIlMHroH9nnhSJm4p9QiQ
mkdir -p $HOME/docker/oracle/oradata
chmod -R 777 $HOME/docker/oracle
sudo docker pull container-registry.oracle.com/database/free:23.4.0.0-lite
sudo docker run -d \
    --name oracleDb \
    -p 1521:1521 \
    -e ORACLE_PWD=test \
    -v $HOME/docker/oracle/oradata:/opt/oracle/oradata \
    -v $SCRIPT_DIR:/script_dir \
    --restart always \
    container-registry.oracle.com/database/free:23.4.0.0-lite
wait_for_docker_container_healthcheck oracleDb
run_in_docker_container oracleDb "sqlplus -s SYSTEM/test < /script_dir/sql/setupdb_oracle.sql"
