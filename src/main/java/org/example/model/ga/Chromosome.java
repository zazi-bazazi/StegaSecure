package org.example.model.ga;

import org.example.model.ga.abstractClasses.AbstractChromosome;
import org.example.model.ga.abstractClasses.AbstractGene;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Chromosome extends AbstractChromosome<ArrayList<AbstractGene<?>>> {
    public Chromosome() {
        this.chromosomeGenes = new ArrayList<>();
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> createEmpty() {
        return new Chromosome();
    }

    @Override
    public void addGene(AbstractGene<?> gene) {
        this.chromosomeGenes.add(gene);
    }

    @Override
    public ArrayList<AbstractGene<?>> getGenes() {
        return this.chromosomeGenes;
    }

    @Override
    public int compareTo(AbstractChromosome o) {
        return Double.compare(this.fitnessScore, o.getFitnessScore());
    }

    @Override
    public AbstractGene<?> getGeneByIndex(int i) {
        return this.chromosomeGenes.get(i);
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeRand(Object... params) {
        int totalBlocks = (int) params[0];
        int messageLength = (int) params[1];

        LinkedHashSet<AbstractGene<?>> uniqueGenes = new LinkedHashSet<>();

        while (uniqueGenes.size() < messageLength) {
            int randomBlock = (int) (Math.random() * totalBlocks);
            int randomCoeff = 6 + (int) (Math.random() * 20); // Targeting mid-frequencies

            uniqueGenes.add(new Gene(randomBlock, randomCoeff));
        }
        Chromosome newChromo = new Chromosome();
        newChromo.chromosomeGenes.addAll(uniqueGenes);

        return newChromo;
    }

    @Override
    public AbstractChromosome<ArrayList<AbstractGene<?>>> generateChromosomeMath(Object... params) {
        int totalBlocks = (int) params[0];
        int L = (int) params[1];

        final int usableCoefficientsPerBlock = 20;
        final int N = totalBlocks * usableCoefficientsPerBlock;

        final int NL = (int) Math.floor((double) N / L);

        List<AbstractGene<?>> generatedGenes = new ArrayList<>();

        for (int i = 0; i < L; i++) {

            //formula: Pos(i) = (i * step) mod N
            int pos = (i * NL) % N;

            // המרה של המיקום הגלובלי חזרה לבלוק ומקדם ספציפי
            int blockIndex = pos / usableCoefficientsPerBlock;

            // שארית החלוקה נותנת את המיקום בתוך הבלוק, נוסיף 6 כדי לפגוע בטווח תדרי הביניים בזיג-זג
            int coeffIndex = 6 + (pos % usableCoefficientsPerBlock);

            generatedGenes.add(new Gene(blockIndex, coeffIndex));
        }

        // 4. בנייה והחזרה של הכרומוזום
        Chromosome newChromo = new Chromosome();
        for(AbstractGene<?> gene : generatedGenes) {
            newChromo.addGene(gene);
        }

        return newChromo;
    }

    public void crossover(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2) {
        AbstractChromosome<?> chroResult = chro1.createEmpty();
        for(int i = 0; i < chro1.getNumGenes(); i++) {
            if (Math.random() < 0.5) {
                chroResult.addGene(chro1.getGeneByIndex(i));
            } else {
                chroResult.addGene(chro2.getGeneByIndex(i));
            }
        }
    }

    @Override
    public void tourmentSelection(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2) {

    }

    @Override
    public void mutate(AbstractChromosome<?> chro1, AbstractChromosome<?> chro2) {

    }
}

