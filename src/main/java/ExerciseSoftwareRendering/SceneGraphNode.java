package ExerciseSoftwareRendering;

import Mesh.*;

import java.util.ArrayList;
import java.util.List;

public class SceneGraphNode {
    Mesh mesh;
    Matrix4x4 transformation;
    List<SceneGraphNode> children = new ArrayList<SceneGraphNode>();

    public SceneGraphNode(Mesh mesh, Matrix4x4 transformation) {
        this.mesh = mesh;
        this.transformation = transformation;
    }


    void render(Matrix4x4 modelMatrix, Matrix4x4 viewProjectionMatrix) {

        Vector3 mNormal;
        Matrix4x4 mvp = modelMatrix.multiply(viewProjectionMatrix);

        // renderTriangles;

        for (SceneGraphNode child : children) {
            child.render(modelMatrix, viewProjectionMatrix);
        }

    }
}
