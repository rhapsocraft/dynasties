# Simulator Specification

### Updating/Upgrading notes
When updating to newer **minecraft** versions. Make sure the following items are reviewed in the new source code to ensure that we are accurately simulating vanilla behaviour.

### When the chunks unload we will then replicate events on the blocks we have chosen to cache

- `Simulator::doRandomTick` replicates random tick implementation of `ServerLevel::tickChunk`
- `CropCacheItem` simulates `CropBlock::randomTick` and `CropBlock::getGrowthSpeed`
- Note that when chunks are unloaded, any block positions/states that aren't cached, will default to `Blocks.AIR` when accessed

### Removal/addition/changing of blocks will also be deferred on the next chunk load if said actions occur while their parent chunks are unloaded

- Prevent notification of neighbors upon the application of deferred states
- Deferred block events rely on caching block states
- Simulation of redstone events is out of scope

