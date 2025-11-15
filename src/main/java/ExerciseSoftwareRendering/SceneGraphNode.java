package ExerciseSoftwareRendering;

import Mesh.Matrix4x4;
import Mesh.*;

import java.util.ArrayList;
import java.util.List;

public class SceneGraphNode {
    Mesh mesh;
    Matrix4x4 transformation;
    List<SceneGraphNode> children = new ArrayList<>();

    public SceneGraphNode(Mesh mesh, Matrix4x4 transformation) {
        this.mesh = mesh;
        this.transformation = transformation;
    }

    void render(Matrix4x4 parentModel, Matrix4x4 viewProjectionMatrix, Exercise7 renderer) {
        Matrix4x4 modelMatrix = Matrix4x4.multiply(parentModel, transformation);
        Matrix4x4 mvp = Matrix4x4.multiply(modelMatrix, viewProjectionMatrix);
        renderer.currentModel = modelMatrix;
        renderer.currentMVP = mvp;

        if (mesh != null) {
            renderer.renderMesh(mesh);
        }

        for (SceneGraphNode child : children) {
            child.render(modelMatrix, viewProjectionMatrix, renderer);
        }
    }
}
