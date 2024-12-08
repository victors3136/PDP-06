public record Vertex(String name) {
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Vertex(String otherName) && this.name.equals(otherName);
    }

    @Override
    public String toString() {
        return name;
    }
}
