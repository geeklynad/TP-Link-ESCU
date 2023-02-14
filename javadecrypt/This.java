package javadecrypt;

public class This {
    private static int[] mS = new int[256];
    private static boolean lC;
    
    public This() {
        super();
    }
    
    private static void aS() {
        if (This.lC) {
            return;
        }
        final String h = new of().h(new byte[] {REDACTED});
        final byte[] array = new byte[256];
        int n = 0;
        for (int i = 0; i < 256; ++i) {
            array[This.mS[i] = i] = (byte)h.charAt(i % h.length());
        }
        for (int j = 0; j < 256; ++j) {
            n = (n + This.mS[j] + array[j]) % 256;
            final int n2 = This.mS[j];
            This.mS[j] = This.mS[n];
            This.mS[n] = n2;
        }
        This.lC = true;
    }
    
    public static byte[] Code(final byte[] array, final int n) {
        if (n > array.length) {
            return null;
        }
        aS();
        int n2 = 0;
        int n3 = 0;
        final int[] array2 = This.mS.clone();
        for (int i = 0; i < n; ++i) {
            n2 = (n2 + 1) % 256;
            n3 = (n3 + array2[n2]) % 256;
            final int n4 = array2[n2];
            array2[n2] = array2[n3];
            array2[n3] = n4;
            final int n5 = (array2[n2] + array2[n3]) % 256;
            final int n6 = i;
            array[n6] ^= (byte)array2[n5];
        }
        return array;
    }
    
    public static byte[] F(final byte[] array) {
        return Code(array, array.length);
    }
    
    public static void main(final String[] array) {
        final byte[] bytes = new String("admin").getBytes();
        Code(bytes, bytes.length);
        System.out.println(TLV.g(bytes));
        for (int length = bytes.length, i = 0; i < length; ++i) {
            System.out.print(bytes[i] + " ");
        }
        Code(bytes, bytes.length);
        System.out.println(TLV.g(bytes));
        for (int length = bytes.length, i = 0; i < length; ++i) {
            System.out.print(bytes[i] + " ");
        }
    }
    
    static {
        This.mS = new int[256];
        This.lC = false;
    }
}
