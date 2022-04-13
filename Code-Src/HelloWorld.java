public class HelloWorld{

	public int fib(int n) {
        if(n < 2){
            return n;
        }
        int[] dp = new int[n+1];
        dp[0] = 0;
        dp[1] = 1;
        for(int i = 2; i < n+1; i++){
            dp[i] = (dp[i-1]+dp[i-2]) % 1000000007;
        }
        return dp[n];
    }

	public static void main(String[] args){
		long start = System.currentTimeMillis();
		HelloWorld hw = new HelloWorld();
		System.out.println(hw.fib(2375));
		long end = System.currentTimeMillis();
		System.out.print(end-start);
	}
	
	
}