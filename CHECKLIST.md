# Update Checklist

When updating to newer **minecraft** versions. Make sure the following items are reviewed in the new source code to ensure that we are accurately simulating vanilla behaviour.

## When the chunks unload we will then replicate events on the blocks we have chosen to cache

- CropCacheItem simulates _**CropBlock::randomTick**_ and _**CropBlock::getGrowthSpeed**_

