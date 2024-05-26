package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;

public class VCFCallingParserFactory {
    private static final CallableSet<ByteCode> parserNameSet = new CallableSet<>();

    static {
        parserNameSet.add(new ByteCode("cutesv"));
        parserNameSet.add(new ByteCode("debreak"));
        parserNameSet.add(new ByteCode("delly"));
        parserNameSet.add(new ByteCode("nanosv"));
        parserNameSet.add(new ByteCode("nanovar"));
        parserNameSet.add(new ByteCode("pbsv"));
        parserNameSet.add(new ByteCode("picky"));
        parserNameSet.add(new ByteCode("sniffles"));
        parserNameSet.add(new ByteCode("svim"));
        parserNameSet.add(new ByteCode("svision"));
        parserNameSet.add(new ByteCode("ukb"));
    }


    public static AbstractCallingParser of(File vcfFile) {
        String fileName = vcfFile.getName();
        String callingParser = getParserName(fileName);
        if (callingParser != null) {
            return getCallingParser(callingParser);
        }
        String fullFilePath = vcfFile.getAbsolutePath();
        callingParser = getParserName(fullFilePath);
        if (callingParser != null) {
            return getCallingParser(callingParser);
        }
        return new StandardVCFCallingParser();
    }
    private static String getParserName(String name){
        String lowerCase = name.toLowerCase();
        for (ByteCode byteCode : parserNameSet) {
            if (lowerCase.contains(byteCode.toString())){
                return byteCode.toString();
            }
        }
        return null;
    }

    private static AbstractCallingParser getCallingParser(String callingName){
        String lowerCase = callingName.toLowerCase();
        switch(lowerCase) {
            case "cutesv":
                return new CuteSVCallingParser();
            case "debreak":
                return new DebreakCallingParser();
            case "delly":
                return new DellyCallingParser();
            case "nanosv":
                return new NanoSVCallingParser();
            case "nanovar":
                return new NanovarCallingParser();
            case "pbsv":
                return new PbsvCallingParser();
            case "picky":
                return new PickyCallingParser();
            case "sniffles":
                return new SnifflesCallingParser();
            case "svim":
                return new SvimCallingParser();
            case "svision":
                return new SvisionCallingParser();
            case "ukb":
                return new UKBBCallingParser();
            default:
                return new StandardVCFCallingParser();
                // Handle default case if needed
        }
    }
}
