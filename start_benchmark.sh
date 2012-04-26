#!/bin/bash

java \
  -Dinfile=/home/fs/kerola/rio_testdata/7976x4480.ppm \
  -Dphases=3 \
  -Dthreads=-1 \
  -DbenchmarkFile=data/benchmark_results \
  -jar dist/image_processing.jar

