# Guide

First run 

```
npm install
```

on your terminal to install all the required node modules.

Secondly, you can simply run the scripts from the package.json, if you're using VS Code, or you can type into your terminal 

```
npm run [TEST]
```

where [TEST] is the filename of the yml file you wish to run.

Please check the scripts in package.json, since there are now scripts that allow you to run several tests in series.

**IMPORTANT**: for testing all workloads sequentially run the script
```
npm run workload
```

# Testing with docker

First run

```
docker build -t tag .
```
to create the Docker image. Next, because of this was tested using GitBash on an Windows environment, you need to run the following command

```
docker run -v '[your path to]\SCC2223\artillery-v2\dockerlogs':/app tag
```

You can find the absolute path by running the command **pwd**.
