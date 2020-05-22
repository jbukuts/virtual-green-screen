# VirtualGreenScreen
Simple Virtual Green Screen written in Java using TensorFlows BodyPix API.

## Info
This small program makes use of the TensorFlow BodyPix API to take in an image and spit back out its corresponding mask using image segmentation. The original idea for this little project comes from a blog post by user BenTheElder. Definitely a cool read.

## Other Stuff
If you would like to increase the video feed's framerate I would reccomended you run your own node server locally and change the source code to reference your localhost. As currently, the java program sends the images from the webcam over to a node backend I have running on a little Google Cloud VM. Another thought is that currently the backend currently uses the faster but less accurate `MobileNet`. In order to increase accuracy just switch the settings in your own Node backend to use BodyPix's `Resnet` (this will cost you framerate).

## Acknowledgements
- BodyPix API -> https://github.com/tensorflow/tfjs-models/tree/master/body-pix
- Blog Post -> https://elder.dev/posts/open-source-virtual-background/

## Todo
- Increase speed of post-processing in Java
- Allow for program to create fake virtual webcam

# Screenshots
