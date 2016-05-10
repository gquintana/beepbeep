package com.github.gquintana.beepbeep.store;

/**
 * DAO, repository to read and write script info to and from database
 * @param <ID> Unique identifier type
 */
public interface ScriptStore<ID> {
    /**
     * Create table, initialize anything
     */
    void prepare();
    /**
     * Read script info by script full name which should be unique
     */
    ScriptInfo<ID> getByFullName(String fullName);

    /**
     * Add, insert new script info, initialize id and version number
     */
    ScriptInfo<ID> create(ScriptInfo<ID> info);

    /**
     * Modify, update existing script info and increment version number
     */
    ScriptInfo<ID> update(ScriptInfo<ID> info);
}
