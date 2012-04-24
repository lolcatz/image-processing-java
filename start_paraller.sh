#!/bin/bash

java \
  -Dinfile=/home/fs/kerola/rio_testdata/7976x4480.ppm \
  -Doutfile=data/7976x4480_out.ppm \
  -Dphases=3 \
  -Dthreads=5 \
  -jar dist/image_processing.jar

