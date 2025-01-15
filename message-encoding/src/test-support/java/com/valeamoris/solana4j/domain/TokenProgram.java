package com.valeamoris.solana4j.domain;

import com.valeamoris.solana4j.Solana;
import com.valeamoris.solana4j.api.PublicKey;
import com.valeamoris.solana4j.programs.Token2022Program;
import com.valeamoris.solana4j.programs.TokenProgramBase;

public enum TokenProgram
{
    TOKEN_PROGRAM("Token",
            "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
            com.valeamoris.solana4j.programs.TokenProgram::factory,
            new com.valeamoris.solana4j.programs.TokenProgram()),
    TOKEN_2022_PROGRAM("Token2022",
            "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb",
            Token2022Program::factory,
            new Token2022Program());

    private final String name;
    private final String address;
    private final TokenProgramInstructionFactory tokenProgramInstructionFactory;
    private final TokenProgramBase tokenProgramBase;

    TokenProgram(final String name,
                 final String address,
                 final TokenProgramInstructionFactory tokenProgramInstructionFactory,
                 final TokenProgramBase tokenProgramBase)
    {
        this.name = name;
        this.address = address;
        this.tokenProgramInstructionFactory = tokenProgramInstructionFactory;
        this.tokenProgramBase = tokenProgramBase;
    }

    public static TokenProgram fromName(final String name)
    {
        for (final TokenProgram tokenProgram : values())
        {
            if (tokenProgram.name.equals(name))
            {
                return tokenProgram;
            }
        }
        throw new RuntimeException("Unrecognised token program!!");
    }

    public TokenProgramInstructionFactory getFactory()
    {
        return tokenProgramInstructionFactory;
    }

    public TokenProgramBase getTokenProgram()
    {
        return tokenProgramBase;
    }

    public PublicKey getProgram()
    {
        return Solana.account(address);
    }
}
