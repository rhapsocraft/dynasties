package com.maestreaux.dynasties.core.production;


import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;

public class Production<P extends Production.IProduct<?>, A extends Production.IAssetAccessor<? extends Production.IAsset<?>>, R extends Production.IRequirements> {
    private final P product;
    private final A asset;
    private final R requirement;

    public Production(P product, A asset, R requirement) {
        this.product = product;
        this.asset = asset;
        this.requirement = requirement;
    }

    public A getAsset() {
        return this.asset;
    }

    public boolean canProduce(AbstractDynastyVillager villager) {
        return this.requirement.fulfillsRequirements(villager);
    }

    public float evaluate(AbstractDynastyVillager villager) {
        return this.product.getGains(villager) - this.requirement.getCost(villager);
    }

    public void performWork(AbstractDynastyVillager villager) { }

    public float getWorkModifier(AbstractDynastyVillager villager) {
        return 1F;
    }

    public boolean canPerformSupportTask(AbstractDynastyVillager villager) {
        return this.requirement.canFulfillRequirements(villager);
    }

    public void produceProduct(AbstractDynastyVillager villager) {
        this.product.produce(villager);
    }

    public R getRequirement() {
        return this.requirement;
    }

    public interface IProduct<I> {
        I getProduct();

        float getGains(AbstractDynastyVillager villager);
        void produce(AbstractDynastyVillager villager);
    }

    public interface IAssetAccessor<B extends IAsset<?>> {
        B lookup(AbstractDynastyVillager villager);

        boolean canUse(B asset, AbstractDynastyVillager villager);
        void attemptUse(B asset, AbstractDynastyVillager villager);
        void work(B asset, Production<?,?,?> production, AbstractDynastyVillager villager);
        boolean canContinueWork(B asset, AbstractDynastyVillager villager);
        boolean isCompleted(B asset, AbstractDynastyVillager villager);
        void setProduction(B asset, Production<?, ?, ?> production, AbstractDynastyVillager villager);
    }

    public interface IRequirements {
        // float getLaborCost(AbstractDynastyVillager villager);
        float getCost(AbstractDynastyVillager villager);
        boolean fulfillsRequirements(AbstractDynastyVillager villager);

        boolean canFulfillRequirements(AbstractDynastyVillager villager);
        void tryFulfillRequirements(ActionDataset actionDataset, AbstractDynastyVillager villager);
        void collectCost(AbstractDynastyVillager villager);
    }

    public interface IAsset<B> {
        B get();
    }

    public static abstract class ActionDataset { }
}
