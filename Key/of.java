package Key;

import java.util.Arrays; //added for verbose output and main testing

public class of
{
    private static final int[] na;
    
    public of() {
        super();
    }
    
    public byte[] Code(final byte[] array, final int n, final int[] array2, final int n2) {
        final int[] v = this.V(array, n);
        int n3 = v[0];
        int n4 = v[1];
        int n5 = 0;
        final int n6 = -1640531527;
        final int n7 = array2[0];
        final int n8 = array2[1];
        final int n9 = array2[2];
        final int n10 = array2[3];
        for (int i = 0; i < n2; ++i) {
            n5 += n6;
            n3 += ((n4 << 4) + n7 ^ n4 + n5 ^ (n4 >> 5) + n8);
            n4 += ((n3 << 4) + n9 ^ n3 + n5 ^ (n3 >> 5) + n10);
        }
        v[0] = n3;
        v[1] = n4;
        return this.Code(v, 0);
    }
    
    public byte[] V(final byte[] array, final int n, final int[] array2, final int n2) {
        final int[] v = this.V(array, n);
        int n3 = v[0];
        int n4 = v[1];
        final int n5 = -1640531527;
        final int n6 = array2[0];
        final int n7 = array2[1];
        final int n8 = array2[2];
        final int n9 = array2[3];
        int n10;
        if (n2 == 32) {
            n10 = -957401312;
        }
        else if (n2 == 16) {
            n10 = -478700656;
        }
        else {
            n10 = n5 * n2;
        }
        for (int i = 0; i < n2; ++i) {
            n4 -= ((n3 << 4) + n8 ^ n3 + n10 ^ (n3 >> 5) + n9);
            n3 -= ((n4 << 4) + n6 ^ n4 + n10 ^ (n4 >> 5) + n7);
            n10 -= n5;
        }
        v[0] = n3;
        v[1] = n4;
        //System.out.println(v[0] + "| |" + v[1]); //use for verbose output
        return this.Code(v, 0);
    }
    
    private int[] V(final byte[] array, final int n) {
        final int[] array2 = new int[array.length >> 2];
        int n2 = 0;
        for (int i = n; i < array.length; i += 4) {
            array2[n2] = (S(array[i + 3]) | S(array[i + 2]) << 8 | S(array[i + 1]) << 16 | array[i] << 24);
            //System.out.print("(" + array[n2] + ") "); //use for verbose output
            n2++;
        }
        return array2;
    }
    
    private byte[] Code(final int[] array, final int n) {
        final byte[] array2 = new byte[array.length << 2];
        int n2 = 0;
        for (int i = n; i < array2.length; i += 4) {
            array2[i + 3] = (byte)(array[n2] & 0xFF);
            array2[i + 2] = (byte)(array[n2] >> 8 & 0xFF);
            array2[i + 1] = (byte)(array[n2] >> 16 & 0xFF);
            array2[i] = (byte)(array[n2] >> 24 & 0xFF);
            //System.out.print(array2[i] + " " + array2[i+1] + " " + array2[i+2] + " " + array2[i+3] + " | "); //use for verbose output
            n2++;
        }
        return array2;
    }
    
    private static int S(final byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        return n;
    }
    
    public byte[] q(final String s) {
        final of of = new of();
        final byte[] bytes = s.getBytes();
        final int n = 8 - bytes.length % 8;
        final byte[] array = new byte[bytes.length + n];
        array[0] = (byte)n;
        System.arraycopy(bytes, 0, array, n, bytes.length);
        final byte[] array2 = new byte[array.length];
        for (int i = 0; i < array2.length; i += 8) {
            System.arraycopy(of.Code(array, i, of.na, 32), 0, array2, i, 8);
        }
        return array2;
    }
    
    public String h(final byte[] array) {
        final of of = new of();
        byte[] v = null;
        final byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i += 8) {
            v = of.V(array, i, of.na, 32);
            System.arraycopy(v, 0, bytes, i, 8);
        }
        final byte offset = bytes[0];
        //System.out.println("Pre-KSA Key in bytes: " + Arrays.toString(bytes)); //use for verbose output
        return new String(bytes, offset, v.length - offset);
    }
    
    public static void main(final String[] array) {
        final String str = "admin";
        System.out.println("Original: " + str);
        final byte[] bytes = str.getBytes();
        for (int length = bytes.length, i = 0; i < length; ++i) {
            System.out.print(bytes[i] + " ");
        }
        System.out.println();
        final of of = new of();
        final byte[] q = of.q(str);
        final String g = TLV.g(q);
        System.out.print("Encrypt: ");
        System.out.println(String.format("%s", g));
        final byte[] array2 = q;
        for (int length2 = array2.length, j = 0; j < length2; ++j) {
            System.out.print(array2[j] + " ");
        }
        System.out.println();
        final String h = of.h(q);
        System.out.print("Decrypt: ");
        System.out.println(h);
        final byte[] bytes2 = h.getBytes();
        for (int length3 = bytes2.length, k = 0; k < length3; ++k) {
            System.out.print(bytes2[k] + " ");
        }
        System.out.println();
    }
    
    static {
        na = new int[] { 2023708229, -158607964, -2120859654, 1167043672 };
    }
}
  
