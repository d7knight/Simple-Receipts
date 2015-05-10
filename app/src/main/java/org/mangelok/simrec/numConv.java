package org.mangelok.simrec;


class constNumtoLetter {

    String[] unitdo = {"", "One", "Two", "Three", "Four", "Five",
        "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
        "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
        "Eighteen", "Nineteen"};
    String[] tens = {"", "Ten", "Twenty", "Thirty", "Forty", "Fifty",
        "Sixty", "Seventy", "Eighty", "Ninety"};
    String[] digit = {"", "Hundred", "Thousand", "Hundred", "Million"};
    int r;

    //Count the number of digits in the input number
    int numberCount(int num) {
        int cnt = 0;

        while (num > 0) {
            r = num % 10;
            cnt++;
            num = num / 10;
        }

        return cnt;
    }

    //Function for Conversion of two digit
    String twonum(int numq) {

        int numr, nq;
        String ltr = "";

        nq = numq / 10;
        numr = numq % 10;

        if (numq > 19) {
            if (!ltr.equals("")) {
                ltr += " ";
            }
            ltr += tens[nq];
            if (numr>0){
                ltr+= " ";
            }
            ltr += unitdo[numr];

        } else {
            
            if (!ltr.equals("")&&numq>0) {
                ltr += " ";
            }
            ltr += unitdo[numq];
        }

        return ltr;
    }

    //Function for Conversion of three digit
    String threenum(int numq) {
        int numr, nq;
        String ltr = "";

        nq = numq / 100;
        numr = numq % 100;

        if (numr == 0) {
            if (!ltr.equals("")) {
                ltr += " ";
            }
            ltr += unitdo[nq] + " " + digit[1];
        } else {
            if (!ltr.equals("")) {
                ltr += " ";
            }
            ltr += unitdo[nq] + " " + digit[1] + " " + twonum(numr);
        }
        return ltr;

    }
}

class numConv {

    public static void main(String[] args) {
        System.out.println(convertPrice(100.00));
    }

    public static String convertPrice(double dPrice) {
        int num, dec;
        num = (int) dPrice;
        dec = (int) Math.rint((dPrice - num) * 100);

        return convertNumber(num) + " Dollars And " + convertNumber(dec) + " Cents";
    }

    public static String convertNumber(int num) {

        //Defining variables q is quotient, r is remainder

        int len, q = 0, r = 0;
        String ltr = "";
        String Str = "";
        constNumtoLetter n = new constNumtoLetter();

        if (num == 0) {
            return "Zero";
        }

        while (num > 0) {


            len = n.numberCount(num);
            
            //Take the length of the number and do letter conversion

            switch (len) {
                case 8:


                case 7://1 000 000
                    q = num / 1000000;
                    r = num % 1000000;
                    ltr = n.twonum(q);
                    if (!Str.equals("")) {
                        Str += " ";
                    }

                    Str += ltr;
                    if (!ltr.equals("")) {
                        Str += " ";
                    }
                    Str += n.digit[4];
                    num = r;
                    break;


                case 6://100000
                    q = num / 100000;
                    r = num % 100000;
                    ltr = n.twonum(q);
                    if (!Str.equals("")) {
                        Str += " ";
                    }
                    Str += ltr;
                    if (!ltr.equals("")) {
                        Str += " ";
                    }
                    Str += n.digit[3];
                    if ((int)(num/1000)%100==0){
                        Str+= " " + n.digit[2];
                    }
                    num = r;
                    break;

                case 5://10 000
                case 4:// 1 000

                    q = num / 1000;
                    r = num % 1000;
                    ltr = n.twonum(q);
                    if (!Str.equals("")) {
                        Str += " ";
                    }
                    Str += ltr;
                    if (!ltr.equals("")) {
                        Str += " ";
                    }
                    Str +=n.digit[2];
                    num = r;

                    break;



                case 2:// 10

                    ltr = n.twonum(num);
                    if (!Str.equals("")) {
                        Str += " ";
                    }
                    Str += ltr;
                    num = 0;
                    break;

                case 1://1
                    if (!Str.equals("")) {
                        Str += " ";
                    }
                    Str += n.unitdo[num];
                    num = 0;
                    break;

                case 3:// 100


                    if (len == 3) {
                        r = num;
                    }
                    ltr = n.threenum(r);
                    if (!Str.equals("")) {
                        Str += " ";
                    }
                    Str += ltr;
                    num = 0;
                    break;
                default:
                    return "Number too large!";


            }
        }


        return Str;
    }
}