# Database for testing

This is example of Microsoft SQL server in Docker (Linux) container, with initialization script which creates database with schema after start-up. It provides a live database for us to test MSG application.

# Running Database

## Build image
To run the demo you just need to build the image (from .dev directory):
```
docker build -t test-db .
```

## Run container
Then, you need to run the container:
```
docker run -p 1433:1433 -d test-db
```
If you want to see logs you can skip _-d_ (_detach_) flag, but then be careful with _CTRL+C_ not to stop the container.
You can also run container in detach mode, then check its name or id with `docker ps` and use:
```
docker logs ContainerIdOrName -f
```

## Connect to database

UserName: sa
Password: Password@1
