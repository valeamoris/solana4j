package com.valeamoris.solana4j.domain;

import com.valeamoris.solana4j.api.TransactionBuilder;
import com.valeamoris.solana4j.programs.TokenProgramBase;

public interface TokenProgramInstructionFactory
{
    TokenProgramBase.TokenProgramBaseFactory factory(TransactionBuilder tb);
}
