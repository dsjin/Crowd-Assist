import logging
import os
import sys

from cloud_handler import CloudLoggingHandler
from cron_executor import Executor

PROJECT = 'senior-project-it' 
TOPIC = 'update_decision_tree'

script_path = os.path.abspath(os.path.join(os.getcwd(), 'tree_task.py'))

sample_task = "python -u %s" % script_path


root_logger = logging.getLogger('cron_executor')
root_logger.setLevel(logging.DEBUG)

cloud_handler = CloudLoggingHandler(on_gce=True, logname="Task_runner")
root_logger.addHandler(cloud_handler)

# create the executor that watches the topic, and will run the job task
executor = Executor(topic=TOPIC, project=PROJECT, task_cmd=sample_task, subname='decision_tree_task')

# add a cloud logging handler and stderr logging handler
job_cloud_handler = CloudLoggingHandler(on_gce=True, logname=test_executor.subname)
executor.job_log.addHandler(job_cloud_handler)
executor.job_log.setLevel(logging.DEBUG)


# watches indefinitely
executorexecutor.watch_topic()
