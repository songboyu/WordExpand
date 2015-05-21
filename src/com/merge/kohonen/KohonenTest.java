package com.merge.kohonen;
import java.util.ArrayList;
import java.util.List;


public class KohonenTest {
	public static int test(int entNum){
		Matrix Koh; 
		Koh = new Matrix(new double[][]{
				{0.6,1},
				{0.1,1},
				{0.4,1},
				{0,1},
				{0,1},
				{0.5,1},
				{0.2,1},
				{0.5,1},
				{0.3,1}
		});
        float sumRoot = Koh.normalizeKohonen() ;
        
        KohonenBuilder kohNet = new KohonenBuilder() ;
        kohNet.makeNetwork(entNum,2);
        Matrix neurons = kohNet.neurons ;
        kohNet.start_learning(Koh) ;
        
        Matrix clusters = Matrix.create(Koh.getWidth(), 1, -1);
        for(int i=0;i<clusters.getWidth();i++){
            float minDist = Float.MAX_VALUE ;
            for(int a=0;a<neurons.getHeigth();a++){
                if(KohonenElements.distance(neurons.getRow(a),Koh.getColumn(i))<minDist){     
                    //System.out.println("Winner i="+i+" a="+a);
                    clusters.setValue(i, 0, a) ;
                    minDist = (float) KohonenElements.distance(neurons.getRow(a),Koh.getColumn(i)) ;
                }
            }
        }
        Matrix X = Matrix.create(clusters.getValueCount(0), Koh.getHeigth(), 0);
        Matrix Y = Matrix.create(clusters.getValueCount(1),Koh.getHeigth(), 0);
        List<Integer> indexX = new ArrayList<Integer>();
        List<Integer> indexY = new ArrayList<Integer>();
        int x = 0;
        int y = 0;
        Koh.denormalizeKohonen(sumRoot);
        for(int i = 0;i < clusters.getWidth(); i++){
        	if(clusters.getValue(i, 0) == 0){
        		for(int j = 0;j < X.getHeigth();j++)
        		{
        			X.setValue(x, j, Koh.getValue(i, j));
        		}
        		x++;
        		indexX.add(i);
        	} else if(clusters.getValue(i, 0) == 1){
        		for(int j = 0;j < Y.getHeigth();j++)
        		{
        			Y.setValue(y, j, Koh.getValue(i, j));
        		}
        		y++;
        		indexY.add(i);
        	}
        }
        List<Integer> indexZ = X.avg() > Y.avg()?indexX:indexY;
        return indexZ.size() > 4?1:0;
//        Matrix Z = X.avg() > Y.avg()?X:Y;
//        System.out.println("X:\n"+X.print());
//        System.out.println("Y:\n"+Y.print());
//        System.out.println(indexZ);
//        int a = 0;
//        System.out.println("Clusters:");
//        System.out.println(clusters.print());
	}
	public static void main(final String[] args){
		for(int a=1;a < 10;a++){
			int count = 0;
			int num = (int)Math.pow(2, a);
			for(int i=0;i<1000;i++){
				
				count += test(num);
			}
			System.out.println(num +"\t"+count/1000.0);
		}
	}
}
