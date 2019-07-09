# Simple Web Server
A simple Java Web Server best used as for quick development. 

DO NOT USE AS A PRODUCTION SEVER!

# How to Use

1. Place jar file in directory you want
2. Create "resources" folder and use that as the root folder for web content
3. Create file named ".routes" in the same folder. Configure as seen fit (See Below)
4. Run and navigate to localhost:5000 browser

# Configure .routes
The .routes file must exist but if it is empty, the server will use file names as routes.
to configure a custom route, add a line similar to the following:

"/routename" : "/filepath" 

NOTE: the router looks to resources as the root path so the "filepath" is relative to the resources folder

