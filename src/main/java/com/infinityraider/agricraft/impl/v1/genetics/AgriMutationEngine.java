package com.infinityraider.agricraft.impl.v1.genetics;

import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.crop.IAgriCrop;
import com.infinityraider.agricraft.api.v1.genetics.*;
import com.infinityraider.agricraft.api.v1.genetics.IAgriMutationEngine;
import com.infinityraider.agricraft.api.v1.plant.IAgriPlant;
import com.infinityraider.agricraft.api.v1.stat.IAgriStat;
import com.infinityraider.agricraft.impl.v1.stats.AgriStatRegistry;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgriMutationEngine implements IAgriMutationEngine {
    private IParentSelector selector;
    private ICloneLogic cloner;
    private ICombineLogic combiner;

    public AgriMutationEngine() {
        this.selector = this::selectAndSortCandidates;
        this.cloner = (parent, random) -> parent.clone();
        this.combiner = (parents, random) -> AgriApi.getAgriGenomeBuilder(parents.getA().getTrait(GeneSpecies.getInstance()))
                .populate(gene -> this.mutateGene(gene, parents, random)).build();
    }

    protected IParentSelector getSelector() {
        return this.selector;
    }

    protected ICloneLogic getCloner() {
        return this.cloner;
    }

    protected ICombineLogic getCombiner() {
        return this.combiner;
    }

    @Override
    public Optional<IAgriPlant> handleMutationTick(IAgriCrop crop, Stream<IAgriCrop> neighbours, Random random) {
        // select candidate parents from the neighbours
        List<IAgriCrop> candidates = this.getSelector().selectAndOrder(neighbours, random);
        // No candidates: do nothing
        if(candidates.size() <= 0) {
            return Optional.empty();
        }
        // Only one candidate: clone
        if(candidates.size() == 1) {
            return this.doClone(crop, candidates.get(0), random);
        }
        // More than one candidate passed, pick the two parents with the highest fertility stat:
        return this.doCombine(crop, candidates.get(0), candidates.get(1), random);
    }

    @Nonnull
    protected List<IAgriCrop> selectAndSortCandidates(Stream<IAgriCrop> neighbours, Random random) {
        return neighbours
                // Valid crops only
                .filter(IAgriCrop::isValid)
                // Mature crops only
                .filter(IAgriCrop::isMature)
                // Fertile crops only
                .filter(IAgriCrop::isFertile)
                // Sort based on fertility stat
                .sorted(Comparator.comparingInt(this::sorter))
                // Roll for fertility stat
                .filter(neighbour -> this.rollFertility(neighbour, random))
                // Collect successful passes
                .collect(Collectors.toList());
    }

    protected Optional<IAgriPlant> doClone(IAgriCrop target, IAgriCrop parent, Random random) {
        IAgriPlant plant = parent.getPlant();
        // Try spawning a clone if cloning is allowed
        if (plant.allowsCloning(parent.getGrowthStage())) {
            // roll for spread chance
            if(random.nextDouble() < parent.getPlant().getSpreadChance(parent.getGrowthStage())) {
                return Optional.of(this.spawnChild(target, plant, this.getCloner().clone(parent.getGenome(), random)));
            }
        }
        // spreading failed
        return Optional.empty();
    }

    protected Optional<IAgriPlant> doCombine(IAgriCrop target, IAgriCrop a, IAgriCrop b, Random random) {
        // Determine the child's genome
        IAgriGenome genome = this.getCombiner().combine(new Tuple<>(a.getGenome(), b.getGenome()), random);
        // Fetch the child's species from the genome
        IAgriPlant plant = genome.getGenePair(AgriGeneRegistry.getInstance().gene_species).getTrait();
        // Spawn the child
        return Optional.of(this.spawnChild(target, plant, genome));
    }

    protected int sorter(IAgriCrop crop) {
        IAgriStat fertility = AgriStatRegistry.getInstance().fertilityStat();
        return fertility.getMax() - crop.getStats().getValue(fertility);
    }

    protected boolean rollFertility(IAgriCrop crop, Random random) {
        IAgriStat fertility = AgriStatRegistry.getInstance().fertilityStat();
        return random.nextInt(fertility.getMax()) < crop.getStats().getValue(fertility);
    }

    protected IAgriPlant spawnChild(IAgriCrop target, IAgriPlant plant, IAgriGenome genome) {
        target.setCrossCrop(false);
        target.setPlant(plant);
        target.setGenome(genome);
        return plant;
    }

    protected <T> IAgriGenePair<T> mutateGene(IAgriGene<T> gene, Tuple<IAgriGenome, IAgriGenome> parents, Random rand) {
        return gene.mutator().pickOrMutate(
                gene,
                this.pickRandomAllel(parents.getA().getGenePair(gene), rand),
                this.pickRandomAllel(parents.getB().getGenePair(gene), rand),
                parents, rand);
    }

    protected  <T> IAllel<T> pickRandomAllel(IAgriGenePair<T> pair, Random random) {
        return random.nextBoolean() ? pair.getDominant() : pair.getRecessive();
    }

    @Override
    public IAgriMutationEngine setSelectionLogic(@Nonnull IParentSelector selector) {
        this.selector = Objects.requireNonNull(selector);
        return this;
    }

    @Override
    public IAgriMutationEngine setCloneLogic(@Nonnull ICloneLogic cloneLogic) {
        this.cloner = Objects.requireNonNull(cloneLogic);
        return this;
    }

    @Override
    public IAgriMutationEngine setCombineLogic(@Nonnull ICombineLogic combineLogic) {
        this.combiner = Objects.requireNonNull(combineLogic);
        return this;
    }
}
