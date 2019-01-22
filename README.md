# CuckooHash

A simple cuckoo hash implementation used to practice hash table concepts

Declaration:

  - cuckooHash\<Type\>(): Declares a new cuckooHash table with size 10, min load 0.2 and max load 0.5
  - cuckooHash\<Type\>(int, int): Declares a new cuckooHash table with size 10, min load int1 and max load int2

Methods:

  - insert(Object): Add specified object to the hash table
  - insert(Object, Boolean): Add specified object to the hash table, passed true in order to bypass automatic resizing
  - delete(Object): Delete table entry for given key
  - get(Object): Return index of given key, automatically offset to allow for single array use
  - getNoOffset(Object): Return index of given key, no offset
  - reHash(): Forcibly rehash the table, updates current table
  - copy(cuckooHash): Perform a deep copy of an existing cuckooHash table
  - toString(): Return a string that shows the state of the two backing arrays
