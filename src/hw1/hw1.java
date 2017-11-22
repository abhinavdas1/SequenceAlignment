package hw1;

import java.io.*;
import java.util.*;

public class hw1 {

    static String[] description;
    static String[] sequence;

    static String[] queryDescription;
    static String[] querySequence;

    static char[] alphabet;
    static Map<Character, Integer> alphabetMap;

    static int[][] mat;

    static PriorityQueue<Sequence> pq;

    static int fault = 0;

    static int k = 0;

    static class Sequence
    {
        String id1, id2;
        char[] sequence1, sequence2;
        int score;
        int start1, start2;
    }

    public static void main(String[] args) {

        String dbFile = args[2] + ".txt";
        String queryFile = args[1] + ".txt";

        int alignmentMethod = Integer.parseInt(args[0]);
        fault = Integer.parseInt(args[6]);;
        k = Integer.parseInt(args[5]);

        readSequence(dbFile, 0);

        File query = new File(queryFile);
        readSequence(queryFile, 1);

        getAlphabet(args[3] + ".txt");

        getMat(args[4] + ".txt");

        if(alignmentMethod == 1) {
            runGlobalAlignment();
        }
         else if(alignmentMethod == 2){
            runLocalAlignment();
        }
        else if(alignmentMethod == 3){
            runDoveTailAlignment();
        }

        for(int i = 0; i < k; i++)
        {
            Sequence temp = pq.poll();
            System.out.println("Score = " + temp.score);
            System.out.print(temp.id1 + " ");
            System.out.print(temp.start1 + " ");
            System.out.println(temp.sequence1);
            System.out.print(temp.id2 + " ");
            System.out.print(temp.start2 + " ");
            System.out.println(temp.sequence2);
        }





    }


    public static void readSequence(String file, int flag)
    {
        List desc= new ArrayList();
        List seq = new ArrayList();
        try{
            BufferedReader in     = new BufferedReader( new FileReader( file ) );
            StringBuffer   buffer = new StringBuffer();
            String         line   = in.readLine();

            if( line == null )
                throw new IOException( file + " is an empty file" );

            if( line.charAt( 0 ) != '>' )
                throw new IOException( "First line of " + file + " should start with '>'" );
            else
                desc.add(line);
            for( line = in.readLine().trim(); line != null; line = in.readLine() )
            {
                if( line.length()>0 && line.charAt( 0 ) == '>' )
                {
                    seq.add(buffer.toString());
                    buffer = new StringBuffer();
                    desc.add(line);
                } else
                    buffer.append( line.trim() );
            }
            if( buffer.length() != 0 )
                seq.add(buffer.toString());
        }catch(IOException e)
        {
            System.out.println("Error when reading "+file);
            e.printStackTrace();
        }

        if(flag == 0)
        {
            description = new String[desc.size()];
            sequence = new String[seq.size()];
            for (int i=0; i< seq.size(); i++)
            {
                description[i]=(String) desc.get(i);
                sequence[i]=(String) seq.get(i);
            }
        }
        else
        {
            queryDescription = new String[desc.size()];
            querySequence = new String[seq.size()];
            for (int i=0; i< seq.size(); i++)
            {
                queryDescription[i]=(String) desc.get(i);
                querySequence[i]=(String) seq.get(i);
            }
        }


    }

    public static void getAlphabet(String file)
    {
        try{
            BufferedReader in     = new BufferedReader( new FileReader( file ) );
            StringBuffer   buffer = new StringBuffer();
            String         line   = in.readLine().toLowerCase();

            alphabet = line.toCharArray();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        alphabetMap = new HashMap<Character, Integer>();

        for(int i = 0; i < alphabet.length; i++)
        {
            alphabetMap.put(alphabet[i], i);
        }
    }

    public static void getMat(String file)
    {
        try{

            mat = new int[alphabet.length][alphabet.length];

            BufferedReader in     = new BufferedReader( new FileReader( file ) );
            StringBuffer   buffer = new StringBuffer();


            for(int i = 0; i < alphabet.length; i++)
            {
                String[] line   = in.readLine().split(" +");
                int count = 0;
                for(int j = 0; j < line.length; j++)
                {
                    mat[i][j] = Integer.parseInt(line[j]);
                }

            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void runGlobalAlignment()
    {
        pq = new PriorityQueue<Sequence>(k, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return o2.score - o1.score;
            }
        });

        for(int i = 0; i < sequence.length; i++)
        {
            for(int j = 0; j < querySequence.length; j++)
            {
                Sequence result = globalAlignment(i, j);
                pq.add(result);
            }
        }

    }

    public static Sequence globalAlignment(int i, int j)
    {
        char[] A = sequence[i].toLowerCase().toCharArray();
        char[] B = querySequence[j].toLowerCase().toCharArray();

        int[][] dp = new int[A.length + 1][B.length + 1];
        int[][] dpDir = new int[A.length + 1][B.length + 1];

        for(int p = 1; p <= A.length; p++)
        {
            dp[p][0] = dp[p-1][0] + fault;
            dpDir[p][0] = 1;
        }

        for(int p = 1; p <= B.length; p++)
        {
            dp[0][p] = dp[0][p-1] + fault;
            dpDir[0][p] = -1;
        }

        for(int p = 1; p <= A.length; p++ )
        {
            for(int q = 1; q <= B.length; q++)
            {
                int a = dp[p-1][q] + fault;
                int b = dp[p][q-1] + fault;
                int c = dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])];
                dp[p][q] = Math.max(dp[p-1][q] + fault, Math.max(dp[p][q-1] + fault, dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])]));
                if(dp[p][q] == a)
                {
                    dpDir[p][q] = 1;
                }
                if(dp[p][q] == b)
                {
                    dpDir[p][q] = -1;
                }
                if(dp[p][q] == c)
                {
                    dpDir[p][q] = 0;
                }

            }

        }

        Sequence s = backtrackTable(dpDir, A, B, dp[A.length][B.length]);
        s.score = dp[A.length][B.length];
        s.id1 = description[i];
        s.id2 = queryDescription[j];


        return s;
    }


    public static Sequence backtrackTable(int[][] dir, char[] A, char[] B, int score)
    {
        Sequence s = new Sequence();

        int s1 = A.length;
        int s2 = B.length;

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        while(s1>0 || s2 > 0){
            if( s1 > 0 && s2 > 0 && dir[s1][s2] == 0 ){
                //Coming from diagonal
                sb1.insert(0, A[s1-1]);
                sb2.insert(0, B[s2-1]);
                s1--;
                s2--;
            }
            else if( s1> 0 && dir[s1][s2] == 1){
                //Coming from vertical
                sb1.insert(0,A[s1-1]);
                sb2.insert(0,'-');
                s1--;
            }
            else if (s2>0 && dir[s1][s2] == -1){
                //Coming from horizontal
                sb1.insert(0,'-');
                sb2.insert(0,B[s2-1]);
                s2--;
            }

        }

        s.sequence1 = sb1.toString().toCharArray();
        s.sequence2 = sb2.toString().toCharArray();

        s.start1 = 0;
        s.start2 = 0;

        s.score = score;

        return s;

    }

    public static void runLocalAlignment()
    {
        pq = new PriorityQueue<Sequence>(k, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return o2.score - o1.score;
            }
        });


        for(int i = 0; i < querySequence.length; i++)
        {
            for (int j = 0; j < sequence.length; j++) {
                Sequence result = localAlignment(j, i);
                pq.add(result);
            }

        }


    }

    public static Sequence localAlignment(int i, int j)
    {
        char[] A = sequence[i].toLowerCase().toCharArray();
        char[] B = querySequence[j].toLowerCase().toCharArray();

        int[][] dp = new int[A.length + 1][B.length + 1];
        int[][] dpDir = new int[A.length + 1][B.length + 1];

        int max = Integer.MIN_VALUE;
        int maxI = 0;
        int maxJ = 0;

        for(int p = 1; p <= A.length; p++)
        {
            dp[p][0] = 0;
            dpDir[p][0] = 0;
        }

        for(int p = 1; p <= B.length; p++)
        {
            dp[0][p] = 0;
            dpDir[0][p] = 0;
        }

        for(int p = 1; p <= A.length; p++ )
        {
            for(int q = 1; q <= B.length; q++)
            {
                int a = dp[p-1][q] + fault;
                int b = dp[p][q-1] + fault;
                int c = dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])];
                dp[p][q] = Math.max(0,Math.max(dp[p-1][q] + fault, Math.max(dp[p][q-1] + fault, dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])])));
                if(a > b && a > c && a > 0)
                {
                    dpDir[p][q] = 1;
                }
                else if(b > c && b > 0)
                {
                    dpDir[p][q] = -1;
                }
                else if(c > 0)
                {
                    dpDir[p][q] = 2;
                }
                else
                {
                    dpDir[p][q] = 0;
                }
                if(dp[p][q] > max)
                {
                    max = dp[p][q];
                    maxI = p;
                    maxJ = q;
                }

            }

        }

        Sequence s = localBacktrackTable(dpDir, A, B, max, dp, maxI, maxJ);
        s.id1 = description[i];
        s.id2 = queryDescription[j];


        return s;
    }


    public static Sequence localBacktrackTable(int[][] dir, char[] A, char[] B, int score, int[][] scoreMat, int maxI, int maxJ)
    {
        Sequence s = new Sequence();

        int s1 = maxI;
        int s2 = maxJ;

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        while(scoreMat[s1][s2] != 0){
            if( dir[s1][s2] == 2 ){
                sb1.insert(0, A[s1-1]);
                sb2.insert(0, B[s2-1]);
                s1--;
                s2--;
            }
            else if(dir[s1][s2] == 1){
                //Coming from vertical
                sb1.insert(0,A[s1-1]);
                sb2.insert(0,'-');
                s1--;
            }
            else if (dir[s1][s2] == -1){
                //Coming from horizontal
                sb1.insert(0,'-');
                sb2.insert(0,B[s2-1]);
                s2--;
            }

        }

        s.sequence1 = sb1.toString().toCharArray();
        s.sequence2 = sb2.toString().toCharArray();

        s.start1 = s1 + 1;
        s.start2 = s2 + 1;

        s.score = score;

        return s;

    }


    public static void runDoveTailAlignment()
    {
        pq = new PriorityQueue<Sequence>(k, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return o2.score - o1.score;
            }
        });

        for(int i = 0; i < querySequence.length; i++)
        {
            for (int j = 0; j < sequence.length; j++) {
                Sequence result = doveTailAlignment(j, i);
                pq.add(result);
            }
        }


    }

    public static Sequence doveTailAlignment(int i, int j)
    {
        char[] A = sequence[i].toLowerCase().toCharArray();
        char[] B = querySequence[j].toLowerCase().toCharArray();

        int[][] dp = new int[A.length + 1][B.length + 1];
        int[][] dpDir = new int[A.length + 1][B.length + 1];

        int max = Integer.MIN_VALUE;
        int maxI = 0;
        int maxJ = 0;

        for(int p = 1; p <= A.length; p++)
        {
            dp[p][0] = 0;
            dpDir[p][0] = 0;
        }

        for(int p = 1; p <= B.length; p++)
        {
            dp[0][p] = 0;
            dpDir[0][p] = 0;
        }

        for(int p = 1; p <= A.length; p++ )
        {
            for(int q = 1; q <= B.length; q++)
            {
                int a = dp[p-1][q] + fault;
                int b = dp[p][q-1] + fault;
                int c = dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])];
                dp[p][q] = Math.max(dp[p-1][q] + fault, Math.max(dp[p][q-1] + fault, dp[p-1][q-1] + mat[alphabetMap.get(A[p - 1])][alphabetMap.get(B[q - 1])]));
                if(dp[p][q] == a)
                {
                    dpDir[p][q] = 1;
                }
                else if(dp[p][q] == b)
                {
                    dpDir[p][q] = -1;
                }
                else if(dp[p][q] == c)
                {
                    dpDir[p][q] = 0;
                }

                if((p == A.length || q == B.length ) &&  (dp[p][q] > max))
                {
                    max = dp[p][q];
                    maxI = p;
                    maxJ = q;
                }

            }

        }

        Sequence s = doveTailBacktrackTable(dpDir, A, B, max, dp, maxI, maxJ);
        s.id1 = description[i];
        s.id2 = queryDescription[j];


        return s;
    }


    public static Sequence doveTailBacktrackTable(int[][] dir, char[] A, char[] B, int score, int[][] scoreMat, int maxI, int maxJ)
    {
        Sequence s = new Sequence();

        int s1 = maxI;
        int s2 = maxJ;

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        while(s1 > 0 && s2 > 0)
        {
            if(dir[s1][s2] == 0 )
            {
                //Coming from diagonal
                sb1.insert(0, A[s1-1]);
                sb2.insert(0, B[s2-1]);
                s1--;
                s2--;
            }
            else if(dir[s1][s2] == 1)
            {
                //Coming from vertical
                sb1.insert(0,A[s1-1]);
                sb2.insert(0,'-');
                s1--;
            }
            else if (dir[s1][s2] == -1)
            {
                //Coming from horizontal
                sb1.insert(0,'-');
                sb2.insert(0,B[s2-1]);
                s2--;
            }

        }

        s.sequence1 = sb1.toString().toCharArray();
        s.sequence2 = sb2.toString().toCharArray();

        s.start1 = s1 + 1;
        s.start2 = s2 + 1;

        s.score = score;

        return s;

    }


}
